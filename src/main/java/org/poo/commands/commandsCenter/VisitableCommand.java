package org.poo.commands.commandsCenter;

/**
 * Interface for a visitable command.
 */
public interface VisitableCommand {
    /**
     * Accepts a visitor.
     * @param visitor - the visitor to accept
     */
    void accept(CommandVisitor visitor);
}
