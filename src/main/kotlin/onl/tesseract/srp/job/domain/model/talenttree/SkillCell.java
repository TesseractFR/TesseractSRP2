package onl.tesseract.srp.job.domain.model.talenttree;


import onl.tesseract.srp.job.domain.model.talent.TalentName;

public record SkillCell(
       TalentName talent
) implements CellType {}
