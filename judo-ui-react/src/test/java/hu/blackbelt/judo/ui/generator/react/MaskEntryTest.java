package hu.blackbelt.judo.ui.generator.react;

import hu.blackbelt.judo.ui.generator.react.mask.MaskEntry;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MaskEntryTest {
    @Test
    void testEmpty() {
        MaskEntry mask = new MaskEntry(Set.of(), null);

        assertEquals("", mask.serialize());
    }

    @Test
    void testOnlyPrimitives() {
        MaskEntry mask = new MaskEntry(Set.of("b", "a", "c"), null);

        assertEquals("a,b,c", mask.serialize());
    }

    @Test
    void testWithRelations() {
        MaskEntry mask = new MaskEntry(Set.of("b", "a", "c"), null);
        MaskEntry rel1 = new MaskEntry(Set.of("j", "f"), "rel1");
        MaskEntry rel2 = new MaskEntry(Set.of("r", "k"), "rel2");
        mask.addRelations(rel1, rel2);

        assertEquals("a,b,c,rel1{f,j},rel2{k,r}", mask.serialize());
    }

    @Test
    void testComplex() {
        MaskEntry mask = new MaskEntry(Set.of("a", "b"), null);
        MaskEntry rel1 = new MaskEntry(Set.of("j", "f"), "rel1");
        MaskEntry rel11 = new MaskEntry(Set.of("x", "i"), "rel11");
        rel1.addRelations(rel11);
        MaskEntry rel111 = new MaskEntry(Set.of("u", "n"), "rel111");
        rel11.addRelations(rel111);
        MaskEntry rel2 = new MaskEntry(Set.of("r", "k"), "rel2");
        mask.addRelations(rel1, rel2);

        assertEquals("a,b,rel1{f,j,rel11{i,x,rel111{n,u}}},rel2{k,r}", mask.serialize());
    }
}
