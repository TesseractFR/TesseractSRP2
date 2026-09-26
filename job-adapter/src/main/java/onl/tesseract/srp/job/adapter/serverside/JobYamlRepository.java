package onl.tesseract.srp.job.adapter.serverside;

import onl.tesseract.srp.job.domain.model.Job;
import onl.tesseract.srp.job.domain.model.JobName;
import onl.tesseract.srp.job.domain.model.Material;
import onl.tesseract.srp.job.domain.model.Sources;
import onl.tesseract.srp.job.domain.model.source.Source;
import onl.tesseract.srp.job.domain.model.source.SourceType;
import onl.tesseract.srp.job.domain.model.talent.Bonus;
import onl.tesseract.srp.job.domain.model.talent.Talent;
import onl.tesseract.srp.job.domain.model.talent.TalentName;
import onl.tesseract.srp.job.domain.model.talent.Talents;
import onl.tesseract.srp.job.domain.port.serverside.JobRepository;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class JobYamlRepository implements JobRepository {

    private final static Path TREEPATH = Path.of("plugins/Tesseract/job");
    private final static String FILE_EXT = ".yaml";

    private final Map<JobName, Job> jobs = new HashMap<>();

    @Override
    public List<Job> findAll() {
        if(jobs.isEmpty()){
            loadJobs();
        }
        return List.copyOf(jobs.values());
    }

    @Override
    public Job findByName(JobName jobName) {
        if(jobs.isEmpty()){
            loadJobs();
        }
        return jobs.get(jobName);
    }


    private void loadJobs(){
        for (Path p : loadFiles()){
            loadJob(p);
        }
    }

    private void loadJob(Path path){
        YamlConfiguration config =  YamlConfiguration.loadConfiguration(path.toFile());
        Job job = Job.builder()
                .jobName(new JobName(config.getString("name")))
                .jobDisplayName(new Job.JobDisplayName(config.getString("display-name")))
                .talents(loadTalents(config.getConfigurationSection("talents")))
                .sources(loadSources(config))
                .build()
                ;
        jobs.put(job.jobName(),job);
    }

    private Talents loadTalents(ConfigurationSection config){
        Talents talents = new Talents();
        for(String key : config.getKeys(false)){
            ConfigurationSection section = config.getConfigurationSection(key);
            if(section == null)continue;
            Talent talent = Talent.builder()
                    .name(new TalentName(key))
                    .maxLevel(section.getInt("maxLevel",1))
                    .bonus(Bonus.valueOf(section.getString("bonus")))
                    .parents(section.getStringList("parents").stream().map(TalentName::new).collect(Collectors.toSet()))
                    .pricePerLevel(loadPricePerLevel(section))
                    .item(new Material(section.getString("item")))
                    .values(section.getDoubleList("values"))
                    .build();
            talents.add(talent.name(),talent);
        }
        return talents;
    }

    private Map<Integer, Integer> loadPricePerLevel(ConfigurationSection config){
        List<Integer> prices = config.getIntegerList("prices");
        Map<Integer, Integer> pricePerLevel = new HashMap<>();
        for(int i = 0; i < prices.size(); i++){
            pricePerLevel.put(i + 1, prices.get(i));
        }
        return pricePerLevel;
    }

    private Sources loadSources(ConfigurationSection config){
        if(config == null) {
            return new Sources(new HashMap<>());
        }
        Map<Source, List<Material>> sources = new HashMap<>();
        ConfigurationSection breakConf = config.getConfigurationSection("break");
        if(breakConf != null){
            for(String key : breakConf.getKeys(false)){
                sources.put(new Source(new Material(key), SourceType.BLOCK), breakConf.getStringList(key).stream().map(Material::new).collect(Collectors.toList()));
            }
        }

        return new Sources(sources);
    }

    private List<Path> loadFiles(){
        if(!Files.exists(TREEPATH) || !Files.isDirectory(TREEPATH))return new ArrayList<>();

        try(Stream<Path> s = Files.list(TREEPATH)) {
             return s.filter(path -> path.toString().endsWith(FILE_EXT))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
