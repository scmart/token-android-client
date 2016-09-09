# Workflow documentation

## Overview

This documents describes the interactions the client has with the server as well as documenting the various cryptographic steps the client has implemented.

## User creation

The first time the app is opened after a fresh install the client needs to register with the backend as well as creating a wallet. At first the wallet is protected by a randomly generated password.

### Communication with the server

 - Client `POST`s to `/user`
 - Server returns with a client id, a salt and an auth token.
 - Client saves all three to SharedPreferences*

### Creating a wallet
 - A random password is generated for the user (via a call to `BCrypt.genSalt()`)
 - Client creates a new wallet using code taken from EthereumJ
 - The private key is AES encrypted using the password
 - The password† and encrypted private key are saved to SharedPreferences
 - The wallet address, encrypted private key and bcrypted password are sent to the server: `PUT /user/:clientid`. The client ID was obtained in the previous step. The auth token from the previous step is also passed as a header.

## Existing pre-onboarded user opening the app

If a user is opening the app but has already gone through the user creation process then things are a little easier. These steps apply to pre-onboarded users -- which means they still have a randomly generated password protecting their private key.

### Recreating the wallet
 - The encrypted private key is read from SharedPreferences
 - The randomly generated password is read from SharedPreferences
 - The private key is decrypted using the password and the wallet is recreated from the wallet.

## Onboarding users

`TODO`

## Onboarded users opening the app

`TODO`



 (*) The implementation of SharedPreferences is actually SecurePreferences which is an encrypted SharedPreferences where they key is stored on the client. It offers some protection for a user but doesn't give much protection against a malicious user. It can essentially be considered plain text.

 (†) Saving the randomly generated to SharedPreferences is necessary during the pre-onboarding state because the user is unaware of the password. It essentially means for a period of time the wallet is unprotected on the users device. We may consider generating a second wallet post-onboarding to ensure the new wallet is fully protected.
