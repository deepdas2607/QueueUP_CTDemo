import { Request, Response } from 'express';

export class ContentController {
  static getQueueTips(req: Request, res: Response) {
    res.json({
      tips: [
        'Arrive a few minutes before your estimated turn.',
        'Keep your notifications enabled to receive turn alerts.',
        'If you need to leave, please cancel your queue entry so others can be served faster.',
        'Check average wait times before selecting a service counter.',
      ],
    });
  }
}
