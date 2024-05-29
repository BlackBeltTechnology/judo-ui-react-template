package hu.blackbelt.judo.ui.generator.react.mask;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MaskEntry {
    private final Set<String> primitives = new LinkedHashSet<>();
    private String relationName;
    private final Set<MaskEntry> relations = new LinkedHashSet<>();

    public MaskEntry(Set<String> primitives, String relationName) {
        if (primitives != null && !primitives.isEmpty()) {
            this.primitives.addAll(primitives);
        }
        if (relationName != null) {
            this.relationName = relationName;
        }
    }

    public String getRelationName() {
        return relationName;
    }

    public MaskEntry setRelationName(String rn) {
        relationName = rn;
        return this;
    }

    public Set<String> getPrimitives() {
        return primitives;
    }

    public Set<MaskEntry> getRelations() {
        return relations;
    }

    public MaskEntry addPrimitives(String... p) {
        primitives.addAll(Set.of(p));
        return this;
    }

    public MaskEntry addPrimitives(Set<String> ps) {
        primitives.addAll(ps);
        return this;
    }

    public MaskEntry addRelations(MaskEntry... r) {
        relations.addAll(Set.of(r));
        return this;
    }

    public MaskEntry addRelations(Set<MaskEntry> rs) {
        relations.addAll(rs);
        return this;
    }

    public String serialize() {
        String p = primitives.stream()
                .sorted()
                .collect(Collectors.joining(","));
        p += (!p.isEmpty() && !relations.isEmpty()) ? "," : "";
        String r = relations.stream()
                .sorted(Comparator.comparing(MaskEntry::getRelationName))
                .map(MaskEntry::serialize)
                .collect(Collectors.joining(","));
        if (relationName != null) {
            return relationName + "{" + p + r + "}";
        }
        return p + r;
    }
}
