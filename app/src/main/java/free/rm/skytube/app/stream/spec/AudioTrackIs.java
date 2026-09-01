/*
 * SkyTube
 * Copyright (C) 2025  SkyTube Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package free.rm.skytube.app.stream.spec;

import org.schabi.newpipe.extractor.stream.AudioStream;
import org.schabi.newpipe.extractor.stream.AudioTrackType;

/**
 * Accepts only audio streams of the given track type.
 */
public final class AudioTrackIs implements StreamSpec<AudioStream> {

    private final AudioTrackType expectedTrackType;

    public AudioTrackIs(AudioTrackType expectedTrackType) {
        this.expectedTrackType = expectedTrackType;
    }

    @Override
    public boolean isSatisfiedBy(AudioStream stream) {
        AudioTrackType trackType = stream.getAudioTrackType();
        // Legacy streams report no track type at all; those are accepted rather than discarded.
        return trackType == null || trackType == expectedTrackType;
    }
}
