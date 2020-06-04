package com.soundcloud.publicApiStrangler.handler.support

/*
 * TODO: this will accept an arg of type com.soundcloud.outcome and output a more descriptive error message.
 *  First the outcome library must be integrated to replace com.soundcloud.publicApiStrangler.support
 */
class UnhandledOutcomeException extends Exception(s"Unhandled outcome")
