# Components changes

- Added CloudDB.CloseConnection() SimpleFunction to explicitly close Redis connections and stop the listener to avoid leaking connections on screen changes or when using private Redis servers with connection limits.

Usage:

- Call `CloseConnection()` when you are done with the CloudDB in your app (for example, when leaving a screen) to ensure connections are released.

Example (blocks-like pseudocode):

1. When Screen1.Hide
    CloudDB1.CloseConnection()

2. When Screen2.Initialize
    // CloudDB will be reinitialized when needed by calling Initialize() or by accessing CloudDB.

Notes:
- CloseConnection prevents automatic listener restart by setting the component's `shutdown` flag. To restart the listener, call the component's `Initialize()` method again.
- This change also ensures the listener's Jedis instance is explicitly closed when stopping the listener.
