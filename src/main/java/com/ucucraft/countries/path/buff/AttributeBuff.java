package com.ucucraft.countries.path.buff;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;

public record AttributeBuff(Attribute attribute, double amount,
                            AttributeModifier.Operation operation, BuffScope scope) implements PathBuff {
}
