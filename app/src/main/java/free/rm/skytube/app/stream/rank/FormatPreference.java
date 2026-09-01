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

package free.rm.skytube.app.stream.rank;

import org.schabi.newpipe.extractor.MediaFormat;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Best-first ordering of the video containers SkyTube is able to play.
 */
public final class FormatPreference implements Comparator<MediaFormat> {

    /** Ordered best-first; also the set of containers accepted for playback. */
    public static final List<MediaFormat> SUPPORTED_VIDEO_CONTAINERS = Collections.unmodifiableList(
            Arrays.asList(MediaFormat.WEBM, MediaFormat.MPEG_4, MediaFormat.v3GPP));

    public static final FormatPreference INSTANCE = new FormatPreference();

    private FormatPreference() {
    }

    @Override
    public int compare(MediaFormat left, MediaFormat right) {
        return Integer.compare(rankOf(left), rankOf(right));
    }

    private static int rankOf(MediaFormat format) {
        int index = SUPPORTED_VIDEO_CONTAINERS.indexOf(format);
        return index >= 0 ? index : Integer.MAX_VALUE;
    }
}
