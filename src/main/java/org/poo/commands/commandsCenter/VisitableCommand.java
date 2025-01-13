package org.poo.commands.commandsCenter;

public interface VisitableCommand {
    /**
     * Accepts a visitor.
     * @param visitor - the visitor to accept
     */
    void accept(CommandVisitor visitor);
}
