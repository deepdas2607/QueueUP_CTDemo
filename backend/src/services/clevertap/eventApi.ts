// FILE TYPE: Backend CleverTap Event API Module
// PURPOSE: Handles server-to-server event logging (e.g. backend queue status changes).
// USED BY: Future event-driven backend workers.
// CURRENT STATUS: Standalone integration module.

import { CleverTapServerService } from './clevertapService';

export class CleverTapEventApi {
  public static async sendServerEvent(identity: string, eventName: string, eventData: Record<string, any>) {
    if (!CleverTapServerService.isConfigured()) {
      return { status: 'skipped', reason: 'CleverTap credentials not configured' };
    }

    const payload = {
      d: [
        {
          identity,
          type: 'event',
          evtName: eventName,
          evtData: eventData,
        },
      ],
    };

    console.log('[CleverTap Server Event API] Track Event Payload:', JSON.stringify(payload));
    return { status: 'success', eventName };
  }
}
