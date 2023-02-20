package hu.blackbelt.judo.ui.generator.react;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class KeyValue<K, V> {
    private K key;
    private V value;
}
