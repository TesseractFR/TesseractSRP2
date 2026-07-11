package onl.tesseract.srp.domain.skill.recipe

import onl.tesseract.srp.domain.item.CustomMaterial
import org.bukkit.Material

interface ComponentWrapper

class CustomComponentWrapper(val customMaterial: CustomMaterial) : ComponentWrapper

class VanillaComponentWrapper(val material: Material) : ComponentWrapper