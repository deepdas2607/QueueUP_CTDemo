// FILE TYPE: Backend CleverTap Service
// PURPOSE: Central server-side gateway interface for CleverTap Server API.
// USED BY: Independent integration layer for server-to-server analytics.
// CURRENT STATUS: Standalone module; not called by Core QueueUp controllers.

import { config } from '../../config/env';

export class CleverTapServerService {
  private static accountId = config.cleverTapAccountId;
  private static passcode = config.cleverTapPasscode;

  public static isConfigured(): boolean {
    return Boolean(this.accountId && this.passcode);
  }

  public static getHeaders(): Record<string, string> {
    return {
      'X-CleverTap-Account-Id': this.accountId,
      'X-CleverTap-Passcode': this.passcode,
      'Content-Type': 'application/json',
    };
  }
}
