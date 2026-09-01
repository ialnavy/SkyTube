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

import org.schabi.newpipe.extractor.stream.Stream;

/**
 * Accepts only streams whose content is a playable URL rather than a manifest.
 */
public final class DirectUrl<T extends Stream> implements StreamSpec<T> {

    @Override
    public boolean isSatisfiedBy(T stream) {
        return stream.isUrl();
    }
}
