package onl.tesseract.srp.customitem.domain.port.serverside;

import onl.tesseract.srp.customitem.domain.model.CustomMaterial;

import java.util.Set;

public interface CustumMaterialRepository {
    Set<CustomMaterial> findAll();
}
