package spellbookgen.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class Spell {
    public String level;
    public String name;
    public String imageName;
    public String school;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Spell spell = (Spell) o;
        return Objects.equals(level, spell.level) && Objects.equals(name, spell.name) && Objects.equals(imageName, spell.imageName) && Objects.equals(school, spell.school);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, name, imageName, school);
    }
}
