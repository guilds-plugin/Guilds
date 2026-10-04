import { expect, test } from '@plugwright/runner';
import { commandWithout } from '../plugins/support/assertions.js';
import { MSG } from '../plugins/support/expected.js';

/**
 * Language selection.
 *
 * ACF is configured with `usePerIssuerLocale(true, false)`, so a player's locale is their own and
 * `/guild language <tag>` changes nobody else's messages. That is what makes this file safe to run
 * alongside the rest of the suite, and it is asserted rather than assumed: the second test proves
 * one bot's locale change leaves another bot on English.
 *
 * Every test here changes the locale of its *own* bot and puts it back before it finishes, so a
 * failure cannot leave the server answering in another language for whoever runs next.
 */

/** The tag of the language the staged config and the rest of the suite run in. */
const ENGLISH = 'en-US';

/** A shipped translation, used as the observable proof that the locale really changed. */
const FRENCH = 'fr-FR';

/** `languages/fr-FR.yml`'s `error.no-guild`, which the English suite never has to see. */
const FRENCH_NO_GUILD = "Vous n'êtes pas dans une guilde !";

/** `languages/fr-FR.yml`'s `error.already-in-guild`, the reply to a second `/guild create`. */
const FRENCH_ALREADY_IN_GUILD = "Vous êtes déjà dans une guilde !";

test('a player can switch language and gets the translated reply', async ({ player, guilds }) => {
    try {
        const switched = player.getMessageBufferIndex();
        await guilds.run(player, `/guild language ${FRENCH}`);
        // The acknowledgement itself is rendered in the new locale, because Guilds sets the locale
        // before sending it.
        await expect(player).toHaveReceivedMessage('Vous avez défini avec succès votre langage en', { since: switched });

        // And a later command comes back translated.
        await commandWithout(player, guilds, '/guild info', FRENCH_NO_GUILD, MSG.noGuild);
    } finally {
        await guilds.run(player, `/guild language ${ENGLISH}`);
    }
});

test('a player in a guild is told so in the language they chose', async ({ player, guilds }) => {
    const guild = await guilds.createGuild(player);

    try {
        await guilds.run(player, `/guild language ${FRENCH}`);
        await expect(player).toHaveReceivedMessage('Vous avez défini avec succès votre langage en');

        // Creating a second guild is refused, and the refusal arrives in French.
        const since = player.getMessageBufferIndex();
        await guilds.run(player, `/guild create ${guilds.guildName()} xx`);
        await expect(player).toHaveReceivedMessage(FRENCH_ALREADY_IN_GUILD, { since });
    } finally {
        await guilds.run(player, `/guild language ${ENGLISH}`);
    }
});

test('one player switching language leaves everyone else in English', async ({ player, createPlayer, guilds }) => {
    const bystander = await createPlayer({ username: guilds.playerName() });

    try {
        await guilds.run(player, `/guild language ${FRENCH}`);
        await expect(player).toHaveReceivedMessage('Vous avez défini avec succès votre langage en');

        // The bystander asked the same thing in the same breath and still gets English.
        const since = bystander.getMessageBufferIndex();
        await guilds.run(bystander, '/guild info');
        await expect(bystander).toHaveReceivedMessage(MSG.noGuild, { since });
        await expect(bystander).not.toHaveReceivedMessage(FRENCH_NO_GUILD, { since });

        // …and the player who switched is still French.
        await commandWithout(player, guilds, '/guild members', FRENCH_NO_GUILD, MSG.noGuild);
    } finally {
        await guilds.run(player, `/guild language ${ENGLISH}`);
    }
});

test('the language actually reverts, not just the acknowledgement', async ({ player, guilds }) => {
    await guilds.run(player, `/guild language ${FRENCH}`);
    await expect(player).toHaveReceivedMessage('Vous avez défini avec succès votre langage en');

    await guilds.run(player, `/guild language ${ENGLISH}`);

    const since = player.getMessageBufferIndex();
    await guilds.run(player, '/guild info');
    await expect(player).toHaveReceivedMessage(MSG.noGuild, { since });
});

test('an unknown language is refused by the command completion', async ({ player, guilds }) => {
    // The language list is built from the files in the plugin's languages directory, so a tag that
    // is not one of them never reaches the handler.
    await commandWithout(
        player,
        guilds,
        '/guild language zz-ZZ',
        MSG.invalidSyntax,
        MSG.languageSet('zz-ZZ'),
    );
});