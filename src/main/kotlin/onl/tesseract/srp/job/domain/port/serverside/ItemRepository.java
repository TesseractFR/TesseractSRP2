package onl.tesseract.srp.job.domain.port.serverside;

import onl.tesseract.srp.job.domain.model.Material;
import onl.tesseract.srp.job.domain.model.PlayerID;
import onl.tesseract.srp.job.domain.model.Quality;
import org.jetbrains.annotations.NotNull;

public interface ItemRepository {
    void giveItem(@NotNull PlayerID playerID, Material material, int quantity, Quality quality);

}
