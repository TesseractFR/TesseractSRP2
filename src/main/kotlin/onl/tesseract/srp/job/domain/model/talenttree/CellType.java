package onl.tesseract.srp.job.domain.model.talenttree;

public sealed interface CellType permits EmptyCell, Arrow, RootCell, SkillCell {}
