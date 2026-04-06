package onl.tesseract.srp.domain.skill.recipe

import org.bukkit.Material

interface ComponentWrapper

class CustomComponentWrapper() : ComponentWrapper
class VanillaComponentWrapper(material : Material) : ComponentWrapper