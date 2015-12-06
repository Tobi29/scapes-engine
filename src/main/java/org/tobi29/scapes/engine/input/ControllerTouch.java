package org.tobi29.scapes.engine.input;

import java8.util.stream.Stream;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2;
import org.tobi29.scapes.engine.utils.math.vector.MutableVector2d;

public interface ControllerTouch extends Controller {
    Stream<Tracker> fingers();

    class Tracker {
        private final MutableVector2 pos = new MutableVector2d();

        public MutableVector2 pos() {
            return pos;
        }
    }
}
