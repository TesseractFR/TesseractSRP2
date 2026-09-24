package onl.tesseract.srp.job.domain.port.serverside;

import lombok.NonNull;
import onl.tesseract.srp.common.domain.model.enums.Quality;
import onl.tesseract.srp.job.domain.model.Material;
import onl.tesseract.srp.job.domain.model.PlayerID;

public interface ItemRepository {
    void giveItem(@NonNull PlayerID playerID, Material material, int quantity, Quality quality);

}
