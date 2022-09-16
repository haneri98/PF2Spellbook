package spellbookgen.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;
import spellbookgen.model.Spell;
import spellbookgen.model.SpellList;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class SpellGenerator {

    static WebDriver driver;
    private SpellList spellList = new SpellList(new ArrayList<>());
    private final static String[] schools = new String[]{"Abjuration", "Conjuration", "Divination",
            "Enchantment", "Evocation", "Illusion", "Necromancy", "Transmutation"};

    public Spell scrapeAndGenerate(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Elements spellScrape = doc.select("span[id=ctl00_RadDrawer1_Content_MainContent_DetailedOutput]");
        Spell spell = new Spell();
        for(Element scrape : spellScrape) {

            String spellLevel = scrapeLevel(scrape);
            String spellName = scrapeName(scrape);
            System.out.println("Spell Name: " + spellName);
            System.out.println("Spell Level: " + spellLevel);
            spell.name = spellName;
            spell.level = spellLevel;
            spell.imageName = spell.name.replace(" ", "_") + ".png";
            spell.id = url.replaceAll("[^0-9]","").substring(1);

            Map<String, String> traitsAndLinks = scrapeTraitsAndLinks(scrape);
            for (String trait : traitsAndLinks.keySet()) {
                if (new ArrayList<>(Arrays.asList(schools)).contains(trait)) {
                    spell.school = trait;
                }
            }
//            System.out.println("Traits: " + traitsAndLinks);
//            String source = scrapeSource(scrape);
//            System.out.println("Source: " + source);
//            String cast = scrapeCast(scrape);
//            System.out.println("Cast: " + cast);
        }

        File test = new File("src/imagesCropped/" + spell.imageName);
        if (!test.exists()) {

            // Initiate Chrome browser
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver();

            // Go to provided spell URL and take a picture of the whole website
            driver.get(url);
            final File screenShotOutputFile = new File("src/images/" + spell.imageName);
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(srcFile, screenShotOutputFile);
            driver.quit();

            // Use python script to crop spell image. Script also saves cropped image
            String[] cmd = {
                    "python",
                    "C:\\Users\\Hans\\IdeaProjects\\SpellbookGenerator\\src\\main\\java\\spellbookgen\\services\\cropper.py",
                    spell.imageName,
            };
            Runtime.getRuntime().exec(cmd);
        }

        if (!spellList.list.contains(spell)) {
            spellList.list.add(spell);
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
            Writer w = new FileWriter("C:\\Users\\Hans\\IdeaProjects\\SpellbookGenerator\\src\\json\\spellList.json");
            gson.toJson(spellList, w);
            w.flush();
            w.close();
        }

        return spell;
    }

    public SpellList getSpellsFromJson() throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        Reader reader = Files.newBufferedReader(Path.of("C:\\Users\\Hans\\IdeaProjects\\SpellbookGenerator\\src\\json\\spellList.json"));
        Type listType = new TypeToken<List<Spell>>() {}.getType();
        SpellList l = gson.fromJson(reader, SpellList.class);
        if (l != null) {
            return l;
        }
        return new SpellList(new ArrayList<>());
    }

    public String getSpellJson() throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        Reader reader = Files.newBufferedReader(Path.of("C:\\Users\\Hans\\IdeaProjects\\SpellbookGenerator\\src\\json\\spellList.json"));
        Object o = gson.fromJson(reader, Object.class);
        return gson.toJson(o);
    }

    public static String scrapeName(Element scrape) {
        String spellLevel = scrape.select("span[style=margin-left:auto; margin-right:0]").text();
        String spellName = scrape.select("h1[class=title]").text().replace(spellLevel, "");
        return spellName;
    }

    public static String scrapeLevel(Element scrape) {
        String s = scrape.select("span[style=margin-left:auto; margin-right:0]").text();
        if (s.contains("Cantrip")) {
            return "Cantrip";
        }
        return s.replaceAll("[^0-9]","");
    }

    public static Map<String, String> scrapeTraitsAndLinks(Element scrape) {
        Map<String, String> traitsAndLinks = new HashMap<>();
        Elements traits = scrape.select("span[class=trait]");
        for (Element trait : traits) {
            String traitName = trait.select("a").text();
            String traitLink = "https://2e.aonprd.com/" + trait.select("a").attr("href");
            traitsAndLinks.put(traitName, traitLink);
        }
        return traitsAndLinks;
    }

    public static String scrapeSource(Element scrape) {
        return scrape.select("a[target=_blank]").text();
    }

    // Everything after source is hard to parse, there is no proper HTML structure
    public static String scrapeCast(Element scrape) {
        String cutoff = scrape.toString();
        if (cutoff.contains("Range")) {
            cutoff = cutoff.substring(0, cutoff.indexOf("Range"));
        } else {
            cutoff = cutoff.substring(0, cutoff.indexOf("Duration"));
        }
        cutoff = cutoff.substring(cutoff.indexOf("Cast</b>") + 8);
        cutoff = cutoff.replace("<span class=\"action action-1\" title=\"Single Action\" role=\"img\" aria-label=\"Single Action\"> </span>", "1-ACT");
        cutoff = cutoff.replace("<span class=\"action action-3\" title=\"Three Actions\" role=\"img\" aria-label=\"Three Actions\"> </span>", "3-ACT");
        cutoff = cutoff.replace("<span class=\"action action-2\" title=\"Two Actions\" role=\"img\" aria-label=\"Two Actions\"> </span>", "2-ACT");
        cutoff = cutoff.replace("<span class=\"action action-4\" title=\"Reaction\" role=\"img\" aria-label=\"Reaction\"> </span>", "REACT");
        cutoff = Jsoup.parse(cutoff).text();
        cutoff = cutoff.replace("Trigger", "Trigger:");
        return cutoff;
    }

    public boolean addSpell(int id) throws IOException {
        this.spellList = this.getSpellsFromJson();
        this.scrapeAndGenerate("https://2e.aonprd.com/Spells.aspx?ID="+id);
        return true;
    }

    public static void main(String[] args) throws IOException {
//        scrapeAndGenerate("https://2e.aonprd.com/Spells.aspx?ID=992");
//        System.out.println("###############");
//        scrapeAndGenerate("https://2e.aonprd.com/Spells.aspx?ID=148");
//        System.out.println("###############");
//        scrapeAndGenerate("https://2e.aonprd.com/Spells.aspx?ID=111");
//        System.out.println("###############");
//        scrapeAndGenerate("https://2e.aonprd.com/Spells.aspx?ID=236");
        SpellGenerator spellgen = new SpellGenerator();
        spellgen.spellList = spellgen.getSpellsFromJson();
        spellgen.scrapeAndGenerate("https://2e.aonprd.com/Spells.aspx?ID=992");
    }
}
