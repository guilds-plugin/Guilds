package me.glaremasters.guilds.economy;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.CommandManager;
import co.aikar.commands.CommandOperationContext;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Stack;

/**
 * Puts a command issuer where ACF's commands look for it.
 *
 * <p>{@code BaseCommand#getCurrentCommandIssuer()} reads the issuer off a thread local stack that
 * the command manager pushes onto while a command runs. The plugin's commands read that issuer to
 * talk to the player, so a test that drives a command directly has to put one there first.
 *
 * <p>The stack is private to ACF, so it is reached by reflection. If a future ACF release moves it,
 * the tests fail loudly here instead of quietly testing nothing.
 */
final class AcfCommandContext implements AutoCloseable {

    private final Stack<CommandOperationContext> stack;

    /**
     * @param manager the command manager the commands should see
     * @param issuer  the issuer the commands should talk to
     */
    @SuppressWarnings("unchecked")
    AcfCommandContext(CommandManager manager, CommandIssuer issuer) {
        try {
            final CommandOperationContext context = Mockito.mock(CommandOperationContext.class);
            Mockito.when(context.getCommandManager()).thenReturn(manager);
            Mockito.when(context.getCommandIssuer()).thenReturn(issuer);

            final Field field = CommandManager.class.getDeclaredField("commandOperationContext");
            field.setAccessible(true);
            final ThreadLocal<Stack<CommandOperationContext>> local =
                    (ThreadLocal<Stack<CommandOperationContext>>) field.get(null);

            this.stack = local.get();
            stack.push(context);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new IllegalStateException("ACF moved its command context stack; the test harness needs updating.", ex);
        }
    }

    @Override
    public void close() {
        stack.pop();
    }
}
