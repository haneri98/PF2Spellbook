package spellbookgen.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SpellList {
    public List<Spell> list;

    public SpellList(List<Spell> list) {
        this.list = list;
    }
}
