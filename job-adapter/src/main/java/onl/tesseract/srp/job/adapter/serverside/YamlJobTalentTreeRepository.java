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
import java.util.Optional;
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
                    grid[y][x] = findCellBySymbol(symbol).orElseGet(() -> toTalentCell(symbol, talentNameMap));
                } else {
                    grid[y][x] = new EmptyCell();
                }
            }
        }

        return grid;
    }

    private CellType toTalentCell(char symbol, Map<Character, TalentName> talentNameMap) {
        return Optional.ofNullable(talentNameMap.get(symbol))
                .<CellType>map(talentName -> talentName.value().equals("ROOT")
                        ? new RootCell()
                        : new SkillCell(talentName))
                .orElseGet(EmptyCell::new);
    }

    private Optional<CellType> findCellBySymbol(char symbol) {
        return switch (symbol) {
            case ' ' -> Optional.of(new EmptyCell());
            case '└' -> Optional.of(ArrowType.TopRight.toArrow());
            case '┘' -> Optional.of(ArrowType.TopLeft.toArrow());
            case '─' -> Optional.of(ArrowType.Horizontal.toArrow());
            case '│' -> Optional.of(ArrowType.Vertical.toArrow());
            case '┬' -> Optional.of(ArrowType.T.toArrow());
            case '┼' -> Optional.of(ArrowType.Cross.toArrow());
            case '┴' -> Optional.of(ArrowType.ReversedT.toArrow());
            case '├' -> Optional.of(ArrowType.RightT.toArrow());
            case '┤' -> Optional.of(ArrowType.LeftT.toArrow());
            default -> Optional.empty();
        };
    }
}
