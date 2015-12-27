package org.tobi29.scapes.engine.input;

import java8.util.function.Predicate;
import java8.util.stream.Collectors;
import java8.util.Optional;
import org.tobi29.scapes.engine.utils.Streams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class ControllerKeyReference {
    private static final Pattern SPLIT = Pattern.compile(",");
    private final ControllerKey key;
    private final List<ControllerKey> modifiers;

    public ControllerKeyReference(ControllerKey key,
            ControllerKey... modifiers) {
        this.key = key;
        this.modifiers = Streams.of(modifiers).collect(Collectors.toList());
    }

    public ControllerKeyReference(ControllerKey key,
            List<ControllerKey> modifiers) {
        this.key = key;
        this.modifiers = modifiers;
    }

    public ControllerKeyReference(List<ControllerKey> keys) {
        if (keys.isEmpty()) {
            throw new IllegalArgumentException(
                    "List requires at least one key");
        }
        key = keys.get(keys.size() - 1);
        modifiers = new ArrayList<>(keys.size() - 1);
        for (int i = 0; i < keys.size() - 1; i++) {
            modifiers.add(keys.get(i));
        }
    }

    public static ControllerKeyReference valueOf(String str) {
        String[] split = SPLIT.split(str, 2);
        List<ControllerKey> modifiers;
        if (split.length > 1) {
            String[] modifierSplit = SPLIT.split(split[1]);
            modifiers = Streams.of(modifierSplit).map(ControllerKey::valueOf)
                    .collect(Collectors.toList());
        } else {
            modifiers = Collections.emptyList();
        }
        return new ControllerKeyReference(ControllerKey.valueOf(split[0]),
                modifiers);
    }

    public static Optional<ControllerKeyReference> isDown(ControllerBasic controller,
            ControllerKeyReference... references) {
        return mostSpecific(reference -> reference.isDown(controller),
                references);
    }

    public static Optional<ControllerKeyReference> isPressed(
            ControllerBasic controller, ControllerKeyReference... references) {
        return mostSpecific(reference -> reference.isPressed(controller),
                references);
    }

    private static Optional<ControllerKeyReference> mostSpecific(
            Predicate<ControllerKeyReference> check,
            ControllerKeyReference... references) {
        int length = -1;
        Optional<ControllerKeyReference> key = Optional.empty();
        for (ControllerKeyReference reference : references) {
            if (check.test(reference) && reference.modifiers.size() > length) {
                key = Optional.of(reference);
                length = reference.modifiers.size();
            }
        }
        return key;
    }

    public boolean isDown(ControllerBasic controller) {
        if (!controller.isDown(key)) {
            return false;
        }
        for (ControllerKey key : modifiers) {
            if (!controller.isDown(key)) {
                return false;
            }
        }
        return true;
    }

    public boolean isPressed(ControllerBasic controller) {
        if (!controller.isPressed(key)) {
            return false;
        }
        for (ControllerKey key : modifiers) {
            if (!controller.isDown(key)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(20);
        str.append(key);
        for (ControllerKey key : modifiers) {
            str.append(',').append(key);
        }
        return str.toString();
    }

    public String humanName() {
        StringBuilder str = new StringBuilder(20);
        str.append(key.humanName());
        for (ControllerKey key : modifiers) {
            str.append(" + ").append(key.humanName());
        }
        return str.toString();
    }
}
