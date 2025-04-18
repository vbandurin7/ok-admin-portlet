// features/portlet/models/portlet-config.model.ts

export interface PortletConfigEntry {
  position: number;
  portletType: string;
}

export interface PortletConfigRequest {
  portlets: PortletConfigEntry[];
}
