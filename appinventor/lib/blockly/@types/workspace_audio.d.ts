/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * Object in charge of loading, storing, and playing audio for a
 *     workspace.
 *
 * @class
 */
import type { WorkspaceSvg } from './workspace_svg.js';
/**
 * Class for loading, storing, and playing audio for a workspace.
 */
export declare class WorkspaceAudio {
    private parentWorkspace;
    /** Database of pre-loaded sounds. */
    private sounds;
    /** Time that the last sound was played. */
    private lastSound;
    /** Whether the audio is muted or not. */
    private muted;
    /** Audio context used for playback. */
    private readonly context?;
    /**
     * @param parentWorkspace The parent of the workspace this audio object
     *     belongs to, or null.
     */
    constructor(parentWorkspace: WorkspaceSvg);
    /**
     * Dispose of this audio manager.
     *
     * @internal
     */
    dispose(): void;
    /**
     * Load an audio file.  Cache it, ready for instantaneous playing.
     *
     * @param filenames Single-item array containing the URL for the sound file.
     *     Any items after the first item are ignored.
     * @param name Name of sound.
     */
    load(filenames: string[], name: string): Promise<void>;
    /**
     * Play a named sound at specified volume.  If volume is not specified,
     * use full volume (1).
     *
     * @param name Name of sound.
     * @param opt_volume Volume of sound (0-1).
     */
    play(name: string, opt_volume?: number): Promise<void>;
    /**
     * @param muted If true, mute sounds. Otherwise, play them.
     */
    setMuted(muted: boolean): void;
    /**
     * @returns Whether the audio is currently muted or not.
     */
    getMuted(): boolean;
}
//# sourceMappingURL=workspace_audio.d.ts.map