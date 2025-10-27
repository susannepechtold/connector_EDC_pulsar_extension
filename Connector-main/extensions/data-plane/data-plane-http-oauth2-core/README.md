# Data-Plane Http OAuth2

This extension can be used when there's a data endpoint that requires OAuth2 authentication through the
[*client credentials flow*](https://auth0.com/docs/get-started/authentication-and-authorization-flow/client-credentials-flow)
It applies on both on **source** and **sink** side of the data transfer, when the data address containes the `oauth2`
related properties, the extension will request a token and add it as a `Bearer` in the `Authorization` header.

Please note that this extension doesn't currently support neither expiration nor refresh tokens, as they are not
mandatory specifications that are up to the OAuth2 server implementation used.

## How to use it

The extension works for all the `HttpData` addresses that contain the "oauth2" properties (defined in
[Oauth2DataAddressSchema](../../../spi/common/oauth2-spi/src/main/java/org/eclipse/edc/iam/oauth2/spi/Oauth2DataAddressSchema.java)).
It supports [both types of client credential](https://connect2id.com/products/server/docs/guides/oauth-client-authentication#credential-types):
shared secret and private-key based.

### Common properties

- `oauth2:tokenUrl`: the url where the token will be requested
- `oauth2:scope`: (optional) the requested scope

### Private-key based client credential

This type of client credential is used when the `HttpData` address contains the `oauth2:privateKeyName` property. This type of client
credential is considered as more secured as described [here](https://connect2id.com/products/server/docs/guides/oauth-client-authentication#private-key-auth-is-more-secure).
The mandatory for working with type of client credentials are:

- `oauth2:privateKeyName`: the name of the private key used to sign the JWT sent to the Oauth2 server
  `oauth2:validity`: the validity of the JWT token sent to the Oauth2 server (in seconds)

### Shared secret client credential

This type of client credential is used when the `HttpData` address DOES not contain the `oauth2:privateKeyName` property.
The mandatory for working with type of client credentials are:

- `oauth2:clientId`: the client id
- `oauth2:clientSecret`: (deprecated) shared secret for authenticating to the Oauth2 server
- `oauth2:clientSecretKey`: the key with which the shared secret for authenticating to the Oauth2 server is stored into the `Vault`

