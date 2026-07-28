package onl.tesseract.srp.job.domain.model;

import lombok.Builder;
import onl.tesseract.srp.job.domain.model.talent.Talents;

@Builder
public record Job(
        JobName jobName,
        JobDisplayName jobDisplayName,
        Talents talents,
        Sources sources
) {



    public record JobDisplayName(String value){

    }


}
