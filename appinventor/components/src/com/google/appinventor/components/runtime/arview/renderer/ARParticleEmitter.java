package com.google.appinventor.components.runtime.arview.renderer;

import android.opengl.Matrix;
import android.util.Log;

import com.google.android.filament.Engine;
import com.google.android.filament.MaterialInstance;
import com.google.android.filament.RenderableManager;
import com.google.android.filament.Scene;
import com.google.android.filament.TransformManager;
import com.google.android.filament.gltfio.Animator;
import com.google.android.filament.gltfio.AssetLoader;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.android.filament.gltfio.FilamentInstance;
import com.google.android.filament.gltfio.ResourceLoader;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ARParticleEmitter — GPU-instanced particle system for Filament 1.9.x.
 *
 * =========================================================================
 * DESIGN
 * =========================================================================
 *
 * Unlike ModelNode / bowling pins (which are independent physics bodies),
 * particles are purely visual. All particles of the same model share one
 * GPU upload via createInstancedAsset. Each particle is one FilamentInstance
 * from the pre-allocated pool, with its own transform updated every frame
 * in a single batch pass.
 *
 * The emitter owns its own pre-allocated FilamentInstance pool. Adding and
 * removing particles never allocates GPU memory — instances are recycled from
 * the pool. This is the pattern createInstancedAsset was designed for.
 *
 * =========================================================================
 * USAGE
 * =========================================================================
 *
 *   // In ARFilamentRenderer, after engine is ready:
 *   ARParticleEmitter rain = new ARParticleEmitter(engine, scene,
 *       assetLoader, resourceLoader);
 *   rain.load(rainDropBuffer, 500);
 *   rain.setEmitterPosition(0, 3f, -2f);   // 3m above, 2m in front
 *   rain.setGravity(-4f);                   // downward pull
 *   rain.setSpread(2f, 0.5f, 2f);           // XYZ spawn spread
 *   rain.setSpeedRange(1f, 3f);
 *   rain.setLifetimeRange(1.5f, 3f);
 *   rain.setEmitRate(50);                   // particles per second
 *   rain.setScale(0.05f);
 *   rain.start();
 *
 *   // Each frame on FilamentRenderThread:
 *   rain.update(deltaTime);
 *
 *   // Cleanup:
 *   rain.destroy();
 *
 * =========================================================================
 * PRESET EFFECTS
 * =========================================================================
 *
 *   ARParticleEmitter.rain(engine, scene, assetLoader, resourceLoader, buffer)
 *   ARParticleEmitter.explosion(engine, scene, assetLoader, resourceLoader, buffer, origin)
 *   ARParticleEmitter.asteroidField(engine, scene, assetLoader, resourceLoader, buffer)
 */
public class ARParticleEmitter {

  private static final String LOG_TAG = "ARParticleEmitter";

  // -------------------------------------------------------------------------
  // Filament references — all touched on FilamentRenderThread only
  // -------------------------------------------------------------------------

  private final Engine         engine;
  private final Scene          scene;
  private final AssetLoader    assetLoader;
  private final ResourceLoader resourceLoader;

  // -------------------------------------------------------------------------
  // Asset and instance pool
  // -------------------------------------------------------------------------

  private FilamentAsset              primaryAsset;
  private FilamentInstance[]         instancePool;
  private int                        poolSize = 0;

  // -------------------------------------------------------------------------
  // Particle state — parallel arrays for cache efficiency
  // -------------------------------------------------------------------------

  /** Current world position of each active particle. */
  private float[][] positions;   // [i][0..2] = x, y, z

  /** Current velocity of each active particle (m/s). */
  private float[][] velocities;  // [i][0..2] = vx, vy, vz

  /** Per-particle rotation quaternion. */
  private float[][] rotations;   // [i][0..3] = x, y, z, w

  /** Per-particle angular velocity (radians/s around each axis). */
  private float[][] angularVels; // [i][0..2] = wx, wy, wz

  /** Remaining lifetime in seconds. Negative = inactive. */
  private float[]   lifetimes;

  /** Max lifetime assigned at spawn (for fade calculation). */
  private float[]   maxLifetimes;

  /** Whether this slot is currently alive. */
  private boolean[] alive;

  /** Number of pre-allocated slots (= poolSize). */
  private int capacity = 0;

  /** Number of currently alive particles. */
  private int activeCount = 0;

  // -------------------------------------------------------------------------
  // Emitter configuration
  // -------------------------------------------------------------------------

  /** World-space origin from which particles are emitted. */
  private float[] emitterPosition = {0f, 0f, 0f};

  /** Spawn spread in each axis (half-extent of spawn box). */
  private float spreadX = 1f, spreadY = 0f, spreadZ = 1f;

  /** Initial velocity direction bias (normalised or zero for omnidirectional). */
  private float[] emitDirection = {0f, -1f, 0f};

  /** Min/max initial speed along emitDirection + random component. */
  private float minSpeed = 1f, maxSpeed = 3f;

  /** Gravity acceleration (m/s² along Y — negative = downward). */
  private float gravity = -9.81f;

  /** Min/max particle lifetime in seconds. */
  private float minLifetime = 1f, maxLifetime = 3f;

  /** Uniform scale applied to all particles. */
  private float particleScale = 1f;

  /** Particles emitted per second. */
  private float emitRate = 20f;

  /** Whether particles spin randomly. */
  private boolean randomRotation = false;

  /** Whether particle scale fades out as lifetime ends. */
  private boolean fadeOut = true;

  /** Accumulated time since last emission. */
  private float emitAccumulator = 0f;

  /** Whether the emitter is currently running. */
  private boolean running = false;

  /** Whether the emitter fires once and stops (explosion mode). */
  private boolean burst = false;
  private boolean burstFired = false;

  /** Number of particles in a burst. */
  private int burstCount = 50;

  /** Animation time for animated models. */
  private float animationTime = 0f;

  private final Random random = new Random();

  // =========================================================================
  // Constructor
  // =========================================================================

  public ARParticleEmitter(Engine engine, Scene scene,
                           AssetLoader assetLoader,
                           ResourceLoader resourceLoader) {
    this.engine         = engine;
    this.scene          = scene;
    this.assetLoader    = assetLoader;
    this.resourceLoader = resourceLoader;
  }

  // =========================================================================
  // SETUP
  // =========================================================================

  /**
   * Load the GLB model and pre-allocate N instances.
   *
   * Must be called on FilamentRenderThread before start().
   *
   * @param buffer   Raw GLB bytes (from modelBufferCache — zero disk I/O).
   * @param maxCount Maximum simultaneous particles. More = more GPU memory.
   *                 Typical values: rain=200, explosion=100, asteroids=50.
   */
  public void load(ByteBuffer buffer, int maxCount) {
    if (primaryAsset != null) {
      Log.w(LOG_TAG, "Already loaded — call destroy() first");
      return;
    }

    instancePool = new FilamentInstance[maxCount];
    buffer.rewind();
    primaryAsset = assetLoader.createInstancedAsset(buffer, instancePool);

    if (primaryAsset == null) {
      Log.e(LOG_TAG, "createInstancedAsset returned null");
      return;
    }

    // Count valid instances
    poolSize = 0;
    for (FilamentInstance inst : instancePool) {
      if (inst != null) poolSize++;
    }

    // Upload textures once — shared by all instances
    resourceLoader.loadResources(primaryAsset);

    // Add primary to scene but move it far away — it's not a visible particle
    scene.addEntity(primaryAsset.getRoot());
    hideEntity(primaryAsset.getRoot());

    // Pre-allocate particle state arrays
    capacity = poolSize;
    positions   = new float[capacity][3];
    velocities  = new float[capacity][3];
    rotations   = new float[capacity][4];
    angularVels = new float[capacity][3];
    lifetimes   = new float[capacity];
    maxLifetimes = new float[capacity];
    alive       = new boolean[capacity];

    // All particles start dead
    for (int i = 0; i < capacity; i++) {
      lifetimes[i] = -1f;
      alive[i]     = false;
      rotations[i][3] = 1f;  // identity quaternion w=1

      // Add each instance's entities to scene, hidden initially
      if (instancePool[i] != null) {
        scene.addEntity(instancePool[i].getRoot());
        for (int e : instancePool[i].getEntities()) {
          if (e != instancePool[i].getRoot()) scene.addEntity(e);
        }
        hideEntity(instancePool[i].getRoot());
      }
    }

    Log.d(LOG_TAG, "Loaded: poolSize=" + poolSize + " capacity=" + capacity);
  }

  // =========================================================================
  // CONFIGURATION — call before start()
  // =========================================================================

  public ARParticleEmitter setEmitterPosition(float x, float y, float z) {
    emitterPosition[0] = x;
    emitterPosition[1] = y;
    emitterPosition[2] = z;
    return this;
  }

  public ARParticleEmitter setSpread(float x, float y, float z) {
    spreadX = x; spreadY = y; spreadZ = z;
    return this;
  }

  public ARParticleEmitter setEmitDirection(float x, float y, float z) {
    float len = (float) Math.sqrt(x*x + y*y + z*z);
    if (len > 0) { x /= len; y /= len; z /= len; }
    emitDirection[0] = x; emitDirection[1] = y; emitDirection[2] = z;
    return this;
  }

  public ARParticleEmitter setSpeedRange(float min, float max) {
    minSpeed = min; maxSpeed = max;
    return this;
  }

  public ARParticleEmitter setGravity(float g) {
    gravity = g;
    return this;
  }

  public ARParticleEmitter setLifetimeRange(float min, float max) {
    minLifetime = min; maxLifetime = max;
    return this;
  }

  public ARParticleEmitter setScale(float scale) {
    particleScale = scale;
    return this;
  }

  public ARParticleEmitter setEmitRate(float particlesPerSecond) {
    emitRate = particlesPerSecond;
    return this;
  }

  public ARParticleEmitter setRandomRotation(boolean enabled) {
    randomRotation = enabled;
    return this;
  }

  public ARParticleEmitter setFadeOut(boolean enabled) {
    fadeOut = enabled;
    return this;
  }

  // =========================================================================
  // CONTROL
  // =========================================================================

  /** Begin continuous emission. */
  public void start() {
    if (primaryAsset == null) {
      Log.w(LOG_TAG, "Cannot start — load() not called");
      return;
    }
    burst = false;
    burstFired = false;
    running = true;
    Log.d(LOG_TAG, "Emitter started");
  }

  /** Fire a one-shot burst of N particles (explosion mode). */
  public void burst(int count) {
    if (primaryAsset == null) return;
    burst = true;
    burstCount = Math.min(count, capacity);
    burstFired = false;
    running = true;
    Log.d(LOG_TAG, "Burst: count=" + burstCount);
  }

  /** Pause emission. Existing particles continue to age and die. */
  public void pause() {
    running = false;
  }

  /** Stop emission and immediately kill all particles. */
  public void stop() {
    running = false;
    for (int i = 0; i < capacity; i++) {
      killParticle(i);
    }
    activeCount = 0;
  }

  // =========================================================================
  // PER-FRAME UPDATE — call on FilamentRenderThread each frame
  // =========================================================================

  /**
   * Advance simulation and update all particle transforms.
   *
   * Call this from ARFilamentRenderer's render loop, on FilamentRenderThread.
   *
   * @param deltaTime seconds since last frame (typically 1/60 or 1/30)
   */
  public void update(float deltaTime) {
    if (primaryAsset == null) return;

    animationTime += deltaTime;

    // ── Emit new particles ────────────────────────────────────────────

    if (running) {
      if (burst && !burstFired) {
        // Fire all burst particles at once
        for (int i = 0; i < burstCount; i++) {
          spawnParticle();
        }
        burstFired = true;
        running = false;  // burst is one-shot
      } else if (!burst) {
        // Continuous emission
        emitAccumulator += emitRate * deltaTime;
        int toEmit = (int) emitAccumulator;
        emitAccumulator -= toEmit;
        for (int i = 0; i < toEmit; i++) {
          spawnParticle();
        }
      }
    }

    // ── Simulate and update transforms ───────────────────────────────

    TransformManager tm = engine.getTransformManager();
    float[] mat = new float[16];

    for (int i = 0; i < capacity; i++) {
      if (!alive[i]) continue;

      // Age particle
      lifetimes[i] -= deltaTime;
      if (lifetimes[i] <= 0f) {
        killParticle(i);
        continue;
      }

      // Physics integration
      velocities[i][1] += gravity * deltaTime;
      positions[i][0]  += velocities[i][0] * deltaTime;
      positions[i][1]  += velocities[i][1] * deltaTime;
      positions[i][2]  += velocities[i][2] * deltaTime;

      // Angular integration
      if (randomRotation) {
        float ax = angularVels[i][0] * deltaTime;
        float ay = angularVels[i][1] * deltaTime;
        float az = angularVels[i][2] * deltaTime;
        float[] dq = axisAngleToQuat(ax, ay, az);
        rotations[i] = multiplyQuat(dq, rotations[i]);
      }

      // Scale fade — shrink to zero as lifetime ends
      float lifeRatio = lifetimes[i] / maxLifetimes[i]; // 1.0 → 0.0
      float s = particleScale * (fadeOut ? lifeRatio : 1f);

      // Build transform matrix
      buildTransformMatrix(mat,
          positions[i][0], positions[i][1], positions[i][2],
          rotations[i], s);

      // Write to this instance's root entity
      if (instancePool[i] != null) {
        int inst = tm.getInstance(instancePool[i].getRoot());
        if (inst != 0) tm.setTransform(inst, mat);
      }

      // Animate if model has animations
      if (instancePool[i] != null) {
        Animator anim = instancePool[i].getAnimator();
        if (anim != null && anim.getAnimationCount() > 0) {
          anim.applyAnimation(0, animationTime);
          anim.updateBoneMatrices();
        }
      }
    }
  }

  // =========================================================================
  // PRIVATE — particle lifecycle
  // =========================================================================

  private void spawnParticle() {
    // Find a dead slot
    int slot = findDeadSlot();
    if (slot < 0) return;  // pool full

    // Position — emitter origin + random spread
    positions[slot][0] = emitterPosition[0] + (random.nextFloat() * 2 - 1) * spreadX;
    positions[slot][1] = emitterPosition[1] + (random.nextFloat() * 2 - 1) * spreadY;
    positions[slot][2] = emitterPosition[2] + (random.nextFloat() * 2 - 1) * spreadZ;

    // Velocity — direction + random component
    float speed = minSpeed + random.nextFloat() * (maxSpeed - minSpeed);
    // Add some randomness perpendicular to main direction
    float rx = (random.nextFloat() * 2 - 1) * 0.3f;
    float ry = (random.nextFloat() * 2 - 1) * 0.3f;
    float rz = (random.nextFloat() * 2 - 1) * 0.3f;
    velocities[slot][0] = (emitDirection[0] + rx) * speed;
    velocities[slot][1] = (emitDirection[1] + ry) * speed;
    velocities[slot][2] = (emitDirection[2] + rz) * speed;

    // Rotation — random if enabled, identity otherwise
    if (randomRotation) {
      float angle = random.nextFloat() * (float)(Math.PI * 2);
      float[] axis = randomUnitVector();
      float[] q = axisAngleToQuat(
          axis[0] * angle, axis[1] * angle, axis[2] * angle);
      rotations[slot] = q;

      // Random angular velocity
      angularVels[slot][0] = (random.nextFloat() * 2 - 1) * 3f;
      angularVels[slot][1] = (random.nextFloat() * 2 - 1) * 3f;
      angularVels[slot][2] = (random.nextFloat() * 2 - 1) * 3f;
    } else {
      rotations[slot][0] = 0f;
      rotations[slot][1] = 0f;
      rotations[slot][2] = 0f;
      rotations[slot][3] = 1f;
      angularVels[slot][0] = 0f;
      angularVels[slot][1] = 0f;
      angularVels[slot][2] = 0f;
    }

    // Lifetime
    float life = minLifetime + random.nextFloat() * (maxLifetime - minLifetime);
    lifetimes[slot]    = life;
    maxLifetimes[slot] = life;
    alive[slot]        = true;
    activeCount++;
  }

  private void killParticle(int slot) {
    if (!alive[slot]) return;
    alive[slot]    = false;
    lifetimes[slot] = -1f;
    activeCount--;
    // Hide by moving far off screen
    if (instancePool[slot] != null) {
      hideEntity(instancePool[slot].getRoot());
    }
  }

  private int findDeadSlot() {
    for (int i = 0; i < capacity; i++) {
      if (!alive[i]) return i;
    }
    return -1;  // all slots occupied
  }

  private void hideEntity(int entity) {
    TransformManager tm = engine.getTransformManager();
    int inst = tm.getInstance(entity);
    if (inst == 0) return;
    float[] hide = new float[16];
    Matrix.setIdentityM(hide, 0);
    hide[13] = -99999f;  // far below scene
    tm.setTransform(inst, hide);
  }

  // =========================================================================
  // CLEANUP
  // =========================================================================

  /**
   * Full cleanup. Call on FilamentRenderThread.
   * After this call the emitter cannot be reused.
   */
  public void destroy() {
    running = false;

    if (primaryAsset == null) return;

    // Remove all instance entities from scene
    if (instancePool != null) {
      for (FilamentInstance inst : instancePool) {
        if (inst == null) continue;
        for (int e : inst.getEntities()) scene.remove(e);
      }
    }

    // Remove primary
    scene.remove(primaryAsset.getRoot());
    for (int e : primaryAsset.getEntities()) scene.remove(e);

    // destroyAsset frees all pre-allocated instances
    assetLoader.destroyAsset(primaryAsset);
    primaryAsset = null;
    instancePool = null;

    Log.d(LOG_TAG, "Emitter destroyed");
  }

  // =========================================================================
  // MATH
  // =========================================================================

  private void buildTransformMatrix(float[] m,
                                    float tx, float ty, float tz,
                                    float[] q, float scale) {
    float x = q[0], y = q[1], z = q[2], w = q[3];
    float x2=x+x, y2=y+y, z2=z+z;
    float xx=x*x2, xy=x*y2, xz=x*z2;
    float yy=y*y2, yz=y*z2, zz=z*z2;
    float wx=w*x2, wy=w*y2, wz=w*z2;

    m[0]  = (1-(yy+zz)) * scale;
    m[1]  = (xy+wz)     * scale;
    m[2]  = (xz-wy)     * scale;
    m[3]  = 0f;
    m[4]  = (xy-wz)     * scale;
    m[5]  = (1-(xx+zz)) * scale;
    m[6]  = (yz+wx)     * scale;
    m[7]  = 0f;
    m[8]  = (xz+wy)     * scale;
    m[9]  = (yz-wx)     * scale;
    m[10] = (1-(xx+yy)) * scale;
    m[11] = 0f;
    m[12] = tx;
    m[13] = ty;
    m[14] = tz;
    m[15] = 1f;
  }

  /**
   * Convert a rotation vector (axis * angle in radians) to quaternion.
   * Input is the product axis*angle — magnitude is the angle.
   */
  private float[] axisAngleToQuat(float ax, float ay, float az) {
    float angle = (float) Math.sqrt(ax*ax + ay*ay + az*az);
    if (angle < 0.0001f) return new float[]{0f, 0f, 0f, 1f};
    float s = (float)(Math.sin(angle * 0.5) / angle);
    float c = (float)  Math.cos(angle * 0.5);
    return new float[]{ ax*s, ay*s, az*s, c };
  }

  private float[] multiplyQuat(float[] a, float[] b) {
    return new float[]{
        a[3]*b[0] + a[0]*b[3] + a[1]*b[2] - a[2]*b[1],
        a[3]*b[1] - a[0]*b[2] + a[1]*b[3] + a[2]*b[0],
        a[3]*b[2] + a[0]*b[1] - a[1]*b[0] + a[2]*b[3],
        a[3]*b[3] - a[0]*b[0] - a[1]*b[1] - a[2]*b[2]
    };
  }

  private float[] randomUnitVector() {
    float x = random.nextFloat() * 2 - 1;
    float y = random.nextFloat() * 2 - 1;
    float z = random.nextFloat() * 2 - 1;
    float len = (float) Math.sqrt(x*x + y*y + z*z);
    if (len < 0.001f) return new float[]{0f, 1f, 0f};
    return new float[]{ x/len, y/len, z/len };
  }

  // =========================================================================
  // GETTERS
  // =========================================================================

  public int getActiveCount()  { return activeCount; }
  public int getCapacity()     { return capacity; }
  public boolean isRunning()   { return running; }

  // =========================================================================
  // PRESET FACTORY METHODS
  // =========================================================================

  /**
   * Continuous rain falling downward from above the camera.
   *
   *   ARParticleEmitter e = ARParticleEmitter.rain(
   *       engine, scene, assetLoader, resourceLoader, rainDropBuffer);
   *   e.update(dt);  // each frame
   */
  public static ARParticleEmitter rain(Engine engine, Scene scene,
                                       AssetLoader assetLoader,
                                       ResourceLoader resourceLoader,
                                       ByteBuffer modelBuffer) {
    ARParticleEmitter e = new ARParticleEmitter(
        engine, scene, assetLoader, resourceLoader);
    e.load(modelBuffer, 300);
    e.setEmitterPosition(0f, 4f, -2f)
        .setSpread(3f, 0.5f, 3f)
        .setEmitDirection(0f, -1f, 0f)
        .setSpeedRange(3f, 6f)
        .setGravity(-9.81f)
        .setLifetimeRange(1f, 2f)
        .setScale(0.02f)
        .setEmitRate(80f)
        .setFadeOut(false)
        .setRandomRotation(false);
    e.start();
    return e;
  }

  /**
   * One-shot explosion bursting outward from a point.
   *
   *   ARParticleEmitter e = ARParticleEmitter.explosion(
   *       engine, scene, assetLoader, resourceLoader,
   *       debrisBuffer, new float[]{0f, 0f, -2f});
   *   e.update(dt);  // each frame until all particles die
   */
  public static ARParticleEmitter explosion(Engine engine, Scene scene,
                                            AssetLoader assetLoader,
                                            ResourceLoader resourceLoader,
                                            ByteBuffer modelBuffer,
                                            float[] origin) {
    ARParticleEmitter e = new ARParticleEmitter(
        engine, scene, assetLoader, resourceLoader);
    e.load(modelBuffer, 80);
    e.setEmitterPosition(origin[0], origin[1], origin[2])
        .setSpread(0.1f, 0.1f, 0.1f)
        .setEmitDirection(0f, 1f, 0f)   // upward bias
        .setSpeedRange(1f, 5f)
        .setGravity(-6f)
        .setLifetimeRange(0.5f, 2f)
        .setScale(0.08f)
        .setRandomRotation(true)
        .setFadeOut(true);
    e.burst(60);
    return e;
  }

  /**
   * Asteroid field drifting slowly toward the camera.
   *
   *   ARParticleEmitter e = ARParticleEmitter.asteroidField(
   *       engine, scene, assetLoader, resourceLoader, rockBuffer);
   *   e.update(dt);
   */
  public static ARParticleEmitter asteroidField(Engine engine, Scene scene,
                                                AssetLoader assetLoader,
                                                ResourceLoader resourceLoader,
                                                ByteBuffer modelBuffer) {
    ARParticleEmitter e = new ARParticleEmitter(
        engine, scene, assetLoader, resourceLoader);
    e.load(modelBuffer, 50);
    e.setEmitterPosition(0f, 0f, -8f)
        .setSpread(5f, 3f, 2f)
        .setEmitDirection(0f, 0f, 1f)   // toward camera
        .setSpeedRange(0.3f, 1.2f)
        .setGravity(0f)                 // space — no gravity
        .setLifetimeRange(6f, 12f)
        .setScale(0.15f)
        .setEmitRate(3f)
        .setRandomRotation(true)
        .setFadeOut(false);
    e.start();
    return e;
  }

  /**
   * Exploding kittens — because why not.
   * Particles burst outward with heavy spin and slow gravity.
   *
   *   ARParticleEmitter e = ARParticleEmitter.explodingKittens(
   *       engine, scene, assetLoader, resourceLoader,
   *       kittenBuffer, new float[]{0f, 0f, -1.5f});
   *   e.update(dt);
   */
  public static ARParticleEmitter explodingKittens(Engine engine, Scene scene,
                                                   AssetLoader assetLoader,
                                                   ResourceLoader resourceLoader,
                                                   ByteBuffer modelBuffer,
                                                   float[] origin) {
    ARParticleEmitter e = new ARParticleEmitter(
        engine, scene, assetLoader, resourceLoader);
    e.load(modelBuffer, 20);
    e.setEmitterPosition(origin[0], origin[1], origin[2])
        .setSpread(0.05f, 0.05f, 0.05f)
        .setEmitDirection(0f, 1f, 0f)
        .setSpeedRange(0.8f, 2.5f)
        .setGravity(-2f)               // floaty — kittens defy gravity slightly
        .setLifetimeRange(2f, 4f)
        .setScale(0.12f)
        .setRandomRotation(true)       // maximum chaos
        .setFadeOut(true);
    e.burst(15);
    return e;
  }
}