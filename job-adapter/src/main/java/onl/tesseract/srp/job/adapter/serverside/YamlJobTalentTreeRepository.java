package onl.tesseract.srp.job.adapter.serverside;

import onl.tesseract.srp.job.domain.model.Job;
import onl.tesseract.srp.job.domain.model.JobName;
import onl.tesseract.srp.job.domain.model.talent.TalentName;
import onl.tesseract.srp.job.domain.model.talenttree.*;
import onl.tesseract.srp.job.domain.port.serverside.JobTalentTreeRepository;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class YamlJobTalentTreeRepository implements JobTalentTreeRepository {

    private final static Path JOB_DIRECTORY = Path.of("plugins/Tesseract/job");
    private final static String FILE_EXT = ".skills";

    private final static Map<JobName, TalentTree> skillTrees = new HashMap<>();

    @Override
    public TalentTree getTalentTree(JobName jobName) {
        if (skillTrees.containsKey(jobName) && false) {
            return skillTrees.get(jobName);
        }
        if (jobName == null) {
            throw new IllegalArgumentException("jobName cannot be null");
        }
        Path file = JOB_DIRECTORY.resolve(jobName.value() + FILE_EXT);
        if(!Files.exists(file)){
            throw new IllegalArgumentException("Fichier non trouvé: " + file);
        }
        List<String> content;
        try{
             content = Files.readAllLines(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        content = content.stream()
                .map(l->l.replace("\r","")
                        .replace("\n",""))
                .toList();
        List<String> graph = content.stream()
                .filter(it-> !it.contains("="))
                .takeWhile(line -> !line.equals("END"))
                .toList().reversed();

        Map<Character, TalentName> talentNameMap = content.stream()
                .takeWhile(line -> !line.startsWith("="))
                .map(line -> line.split("="))
                .collect(Collectors.toMap(
                        arr -> arr[0].charAt(0),
                        arr -> new TalentName(arr[1])
                ));

        CellType[][] grid2D = parseGraphToGrid(graph,talentNameMap);
        TalentTree skillTree = new TalentTree(grid2D);
        skillTrees.put(jobName, skillTree);
        return skillTree;
    }

    private CellType[][] parseGraphToGrid(List<String> graphLines, Map<Character, TalentName> talentNameMap) {
        if (graphLines.isEmpty()) {
            return new CellType[0][0];
        }

        int height = graphLines.size();
        int width = graphLines.stream()
                .mapToInt(String::length)
                .max()
                .orElse(0);

        CellType[][] grid = new CellType[height][width];

        for (int y = 0; y < height; y++) {
            String line = graphLines.get(y);
            for (int x = 0; x < width; x++) {
                if (x < line.length()) {
                    char symbol = line.charAt(x);
                    grid[y][x] = getFromString(symbol);
                    if(grid[y][x] == null) {
                        TalentName talentName = talentNameMap.get(symbol);
                        if(talentName == null){
                            grid[y][x] = new EmptyCell();
                            continue;
                        }
                        if(talentName.value().equals("ROOT")){
                            grid[y][x] = new RootCell();
                            continue;
                        }
                        grid[y][x] = new SkillCell(talentName);
                    }
                } else {
                    grid[y][x] = new EmptyCell();
                }
            }
        }

        return grid;
    }

    private CellType getFromString(char s) {
        return switch (s) {
            case ' ' -> new EmptyCell();
            case '└' -> ArrowType.TopRight.toArrow();
            case '┘' -> ArrowType.TopLeft.toArrow();
            case '─' -> ArrowType.Horizontal.toArrow();
            case '│' -> ArrowType.Vertical.toArrow();
            case '┬' -> ArrowType.T.toArrow();
            case '┼' -> ArrowType.Cross.toArrow();
            case '┴' -> ArrowType.ReversedT.toArrow();
            case '├' -> ArrowType.RightT.toArrow();
            case '┤' -> ArrowType.LeftT.toArrow();
            default -> null;
        };
    }
}
