// features/portlet/models/portlet-config.model.ts

export interface PortletConfigEntry {
  position: number;
  portletType: string;
}

export interface PortletConfigRequest {
  portlets: PortletConfigEntry[];
}

export interface PortletStatRecord {
    timestampOrDay: string;
    impressions: number;
    clicks: number;
    skips: number;
    ctr: number;
    skipRate: number;
}

export interface PortletStatsResponse {
    lastDayStats: PortletStatRecord[];
    lastWeekStats: PortletStatRecord[];
}

export interface ResolverConfigsResponse {
  resolverConfigs: Record<string, string[]>;
}

export interface EnableConfigResponse {
    result: string;
    configName?: string;
}

