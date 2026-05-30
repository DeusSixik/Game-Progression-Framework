package dev.sixik.gpf.utils;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public record ArrayCodec<E>(
        Codec<E> elementCodec,
        IntFunction<E[]> arrayFactory,
        int minSize,
        int maxSize
) implements Codec<E[]> {

    private <R> DataResult<R> createTooShortError(final int size) {
        return DataResult.error(() -> "Array is too short: " + size + ", expected range [" + minSize + "-" + maxSize + "]");
    }

    private <R> DataResult<R> createTooLongError(final int size) {
        return DataResult.error(() -> "Array is too long: " + size + ", expected range [" + minSize + "-" + maxSize + "]");
    }

    @Override
    public <T> DataResult<T> encode(final E[] input, final DynamicOps<T> ops, final T prefix) {
        if (input.length < minSize) {
            return createTooShortError(input.length);
        }
        if (input.length > maxSize) {
            return createTooLongError(input.length);
        }

        final ListBuilder<T> builder = ops.listBuilder();
        for (final E element : input) {
            builder.add(elementCodec.encodeStart(ops, element));
        }
        return builder.build(prefix);
    }

    @Override
    public <T> DataResult<Pair<E[], T>> decode(final DynamicOps<T> ops, final T input) {
        return ops.getList(input).setLifecycle(Lifecycle.stable()).flatMap(stream -> {
            final DecoderState<T> decoder = new DecoderState<>(ops);
            stream.accept(decoder::accept);
            return decoder.build();
        });
    }

    @Override
    public String toString() {
        return "ArrayCodec[" + elementCodec + ']';
    }

    private class DecoderState<T> {
        private static final DataResult<Unit> INITIAL_RESULT = DataResult.success(Unit.INSTANCE, Lifecycle.stable());

        private final DynamicOps<T> ops;
        private final List<E> elements = new ArrayList<>();
        private final Stream.Builder<T> failed = Stream.builder();
        private DataResult<Unit> result = INITIAL_RESULT;
        private int totalCount;

        private DecoderState(final DynamicOps<T> ops) {
            this.ops = ops;
        }

        public void accept(final T value) {
            totalCount++;
            if (elements.size() >= maxSize) {
                failed.add(value);
                return;
            }
            final DataResult<Pair<E, T>> elementResult = elementCodec.decode(ops, value);
            elementResult.error().ifPresent(error -> failed.add(value));
            elementResult.resultOrPartial().ifPresent(pair -> elements.add(pair.getFirst()));
            result = result.apply2stable((result, element) -> result, elementResult);
        }

        public DataResult<Pair<E[], T>> build() {
            if (elements.size() < minSize) {
                return createTooShortError(elements.size());
            }

            final T errors = ops.createList(failed.build());
            final E[] array = elements.toArray(arrayFactory);
            final Pair<E[], T> pair = Pair.of(array, errors);

            if (totalCount > maxSize) {
                result = createTooLongError(totalCount);
            }
            return result.map(ignored -> pair).setPartial(pair);
        }
    }
}
