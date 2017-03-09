# Simply Credit Mailer

Utility / application for providing Simply Credit email automation services. Message requests go in; emails go out.
All emails are ultimately sent via Mailgun. This utility simply submits the request to Mailgun via their REST API.

Mileage may vary.

## Configuration

By default, this application uses the configuration data stored in resource/config.edn. For connecting to Mailgun,
you'll need to set the following environment variables in your terminal session before executing the Lein commands
referenced in subsequent sections:
- MAILGUN_API_KEY
- MAILGUN_FROM
- MAILGUN_ENDPOINT

Alternatively, you can update the config file, swapping out the `#env MAILGUN_API_KEY` (et. al.) bits for the actual
property values. Just be sure to avoid committing these to source control.

## Part 1: CLI

Sends basic email requests via a command line interface. Usage:
`lein send-message [to-address] [subject] [body]`

Example:
`lein send-message send-message stephenmhopper@gmail.com "How you doin'?" "Sup?"`

## Part 2: Template support

Sends basic email requests via a command line interface (just like part 1), but makes use of templates. Usage:
`lein send-message [template-name] [to-address] [subject] [body]`

Examples:
`lein send-message send-template "welcome" "stephenmhopper@gmail.com" "Welcome to the Jungle" "name" "Stephen"`
`lein send-message send-template "password_reset" "stephenmhopper@gmail.com" "Password Reset Request" "name" "Stephen" "url" "http://www.google.com"`

For usage instructions, just run `lein send-message` without any arguments.

Currently supported templates:
- welcome
-- params:
--- name: The name of the individual receiving the welcome email.
- password_reset
-- params:
--- name: The name of the individual receiving the password reset.
--- url: The URL the individual should visit to finish the password reset process.

## Part 3: Web REST API

Starts up a web server for processing email requests via HTTP calls. Usage:
`lein ring server-headless`

This should start the server on port 3000.

Endpoints:
`POST /send-message` - Sends an email message based on a JSON post request. The `to` address, `subject` line, and `body`
should be specified in the body of a JSON object like so:
```
{
"to": "stephenmhopper@gmail.com",
"subject": "How you doin'?",
"body": "Sup?"
}
```

`POST /send-template` - Sends an email message based on a template. Accepts JSON POST requests. The `to` address, `subject`
line, and `template` name should be specified. Additional required arguments should be specified in an additional, required
`args` property map. Example:
```
{
"to": "stephenmhopper@gmail.com",
"subject": "Welcome to the Jungle",
"template": "welcome",
"args": {
    "name": "Stephen"
  }
}
```

## Part 4: Queue processing

This application uses Onyx to read message requests from Kafka and submit them to Mailgun on demand. Invalid messages
are written to a backout / error topic. Valid messages are transformed and then submitted to Mailgun.

To start the required Zookeeper and Kafka services, do the following:
1. Install Docker native.
2. In a terminal session, `cd` into this project's `scripts` directory.
3. Run `bash start_dev.sh`. This will pull down the necessary images and give you a Kafka cluster of size three.

All other interactions with Onyx and the application can be managed via the custom `message-queue` CLI.
By default, this tool uses the properties defined in resources/config.edn, but does support other config files via
the `--config` option.

To start sending emails, do the following:
1. Create the necessary Kafka topics by running `lein message-queue create-topics`.
2. Start up your Onyx peers / executors by running `lein message-queue start-peers 8` (though anything greater than five should be fine).
3. (Optional) In a separate window, run `tail -f onyx.log` to read what Onyx has to say.
4. In a separate window, submit your Onyx job for processing mail messages with `lein message-queue submit-job mailer`.
Please note that this job always starts at the latest offset within the topic. That is, no messages submitted to the
topic before the job started will be processed. Please see the next steps section for more details on this.
5. Submit some email requests. You can send plain message requests like so: `lein message-queue submit-message [to-address] [subject] [body]`.
You can send template message requests with this: `lein message-queue submit-template [template-name] [to-address] [subject] [arg-pairs]`.
(Please note that `arg-pairs` is a list of key value pairs like in the original CLI tool (see Part 2 for more details).
6. Wait for the app to pick them up and submit them to Mailgun. You should see some messages in the output of your
"start-peers" window as it submits the messages to Mailgun.
7. Wait for Mailgun to email the messages to your clients.

## Requirements
- Java 8
- Leiningen
- Docker native
- A connection to the Internet

## Shortcomings / Future Work
1. Unit tests. Every good project has unit tests. This one needs more time.
2. Improved verification / error handling. For The CLI, there's not much to do here. The web portion could benefit from
some pre-validation steps / helpful error messages. For example, if the user requests to send an email with a template
that doesn't exist, we should give them some sort of 400 error with a helpful message about the template not existing.
Similarly, if they submit a template request, but leave off some of the parameters, we should probably tell them what's
missing. The Onyx version of the application does some checking of requests, but just forwards the bad ones off to
another topic. It'd be nice to have our own custom message format for this topic which includes the original message,
an error message, and any other metadata that might be useful (process time, machine / process name, etc.). Finally,
we don't have any error handling around Mailgun. If our HTTP call to Mailgun fails, the whole thing barfs. This isn't
good in the long run and would also benefit from improved error handling / messaging.

## License

Copyright © 2017 Stephen M. Hopper

Distributed under the Eclipse Public License, the same as Clojure.
