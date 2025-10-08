package onl.tesseract.srp.domain.craftmanship

import org.bukkit.Material

interface ComponentWrapper

class CustomComponentWrapper() : ComponentWrapper
class VanillaComponentWrapper(material : Material) : ComponentWrapper