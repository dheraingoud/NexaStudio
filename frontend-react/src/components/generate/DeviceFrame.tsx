import React from 'react';
import { cn } from '../../lib/utils';

/* ═══════════════════════════════════════════════════════════════════
   Types
   ═══════════════════════════════════════════════════════════════════ */

export type DeviceView = 'desktop' | 'tablet' | 'mobile';

export interface DeviceFrameProps {
  /** Content to display inside the device frame */
  children: React.ReactNode;
  /** Device type to render */
  device: DeviceView;
  /** URL to display in the address bar */
  url?: string;
}

/* ═══════════════════════════════════════════════════════════════════
   Device Dimensions
   ═══════════════════════════════════════════════════════════════════ */

const DEVICE_DIMENSIONS: Record<DeviceView, { width: string; height: string }> = {
  desktop: { width: '100%', height: '100%' },
  tablet: { width: '768px', height: '1024px' },
  mobile: { width: '375px', height: '812px' },
};

/* ═══════════════════════════════════════════════════════════════════
   DeviceFrame Component
   macOS-style browser window frame with traffic light buttons
   ═══════════════════════════════════════════════════════════════════ */

const DeviceFrame = React.memo(function DeviceFrame({
  children,
  device,
  url,
}: DeviceFrameProps) {
  const dimensions = DEVICE_DIMENSIONS[device];

  return (
    <div className="w-full h-full flex items-center justify-center p-4 bg-[#09080e]">
      <div
        className={cn(
          'bg-[#0c0a12] rounded-xl overflow-hidden border border-lilac-200/10 flex flex-col shadow-2xl shadow-black/40',
          device !== 'desktop' && 'max-h-full'
        )}
        style={{
          width: dimensions.width,
          height: dimensions.height,
          maxWidth: '100%',
          maxHeight: '100%',
        }}
      >
        {/* macOS-style title bar */}
        <div className="h-9 border-b border-lilac-200/8 flex items-center px-3 gap-3 shrink-0 bg-[#0e0c14]/80 backdrop-blur-sm">
          {/* Traffic light buttons */}
          <div className="flex gap-1.5" aria-hidden="true">
            <div className="w-2.5 h-2.5 rounded-full bg-[#ff5f57]" title="Close" />
            <div className="w-2.5 h-2.5 rounded-full bg-[#febc2e]" title="Minimize" />
            <div className="w-2.5 h-2.5 rounded-full bg-[#28c840]" title="Maximize" />
          </div>

          {/* Address bar */}
          <div className="flex-1 flex items-center justify-center">
            <div className="flex items-center gap-2 px-3 py-1 rounded-md bg-lilac-200/5 text-[11px] text-lilac-400/40 max-w-md w-full">
              <span className="truncate">{url || 'localhost:3000'}</span>
            </div>
          </div>

          {/* Spacer for symmetry */}
          <div className="w-16" />
        </div>

        {/* Content area */}
        <div className="flex-1 overflow-hidden">{children}</div>
      </div>
    </div>
  );
});

export default DeviceFrame;
