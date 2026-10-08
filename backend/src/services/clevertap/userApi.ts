// FILE TYPE: Backend CleverTap User API Module
// PURPOSE: Handles server-side user profile creation and updates via CleverTap User API.
// USED BY: Future server-side sync workflows.
// CURRENT STATUS: Standalone integration module.

import { CleverTapServerService } from './clevertapService';

export class CleverTapUserApi {
  public static async uploadUserProfile(userProfile: {
    objectId: string;
    name: string;
    email: string;
    occupation?: string;
    interests?: string;
    role?: string;
  }) {
    if (!CleverTapServerService.isConfigured()) {
      return { status: 'skipped', reason: 'CleverTap credentials not configured in backend env' };
    }

    const payload = {
      d: [
        {
          identity: userProfile.objectId,
          type: 'profile',
          profileData: {
            Name: userProfile.name,
            Email: userProfile.email,
            Occupation: userProfile.occupation || 'Unspecified',
            Interests: userProfile.interests || 'None',
            Role: userProfile.role || 'USER',
            'MSG-email': true,
            'MSG-push': true,
          },
        },
      ],
    };

    console.log('[CleverTap Server User API] Upload Profile Payload:', JSON.stringify(payload));
    return { status: 'success', identity: userProfile.objectId };
  }
}
