package spellbookgen.controller;

import com.google.gson.JsonElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import spellbookgen.services.SpellGenerator;

import java.io.IOException;

@RequestMapping("")
@RestController
public class IndexController {

    @Autowired
    SpellGenerator service;

    @GetMapping
    public String index(){
        return "API response";
    }

    @GetMapping(path="spells", produces= MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public String getSpells() throws IOException {
        return service.getSpellJson();
    }

    @GetMapping("add")
    @CrossOrigin
    public boolean addSpell(@RequestParam(value = "id") Integer id) throws IOException {
        return service.addSpell(id);
    }

}
