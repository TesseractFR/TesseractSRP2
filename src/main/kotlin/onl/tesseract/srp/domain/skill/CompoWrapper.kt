package onl.tesseract.srp.domain.skill

import org.bukkit.Material

interface ComponentWrapper

class CustomComponentWrapper() : ComponentWrapper
class VanillaComponentWrapper(material : Material) : ComponentWrapper