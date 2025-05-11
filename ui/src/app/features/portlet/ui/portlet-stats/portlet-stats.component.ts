import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  computed,
  effect,
  inject,
  signal
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PortletConfigApiService } from 'features/portlet/api';
import { PortletStatsResponse, PortletStatRecord } from 'features/portlet/models';

@Component({
  standalone: true,
  selector: 'app-portlet-stats',
  templateUrl: './portlet-stats.component.html',
  styleUrls: ['./portlet-stats.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [PortletConfigApiService],
  imports: [CommonModule, FormsModule]
})
export class PortletStatsComponent {
  private readonly portletApi = inject(PortletConfigApiService);
  private readonly cdr = inject(ChangeDetectorRef);

  portletType = '';
  stats = signal<PortletStatsResponse | null>(null);
  isLoading = signal(false);
  loaded = signal(false);
  error = signal<string | null>(null);
  showButtons = signal(false);
  showLastDayStats = signal(true);
  showLastWeekStats = signal(false);

  private readonly lastDayStatsSignal = computed<PortletStatRecord[]>(
    () => this.stats()?.lastDayStats ?? []
  );

  private readonly lastWeekStatsSignal = computed<PortletStatRecord[]>(
    () => this.stats()?.lastWeekStats ?? []
  );

  get lastDayStats(): PortletStatRecord[] {
    return this.lastDayStatsSignal();
  }

  get lastWeekStats(): PortletStatRecord[] {
    return this.lastWeekStatsSignal();
  }

  toggleLastDayStats() {
    this.showLastDayStats.set(true);
    this.showLastWeekStats.set(false);
  }

  toggleLastWeekStats() {
    this.showLastDayStats.set(false);
    this.showLastWeekStats.set(true);
  }

  fetchStats() {
    this.isLoading.set(true);
    this.error.set(null);

    this.portletApi.getPortletStats(this.portletType)
      .then(response => {
        this.stats.set(response);
        this.showButtons.set(true);
        this.loaded.set(true);

      })
      .catch(err => {
        this.error.set('Ошибка загрузки статистики');
      })
      .finally(() => {
        this.isLoading.set(false);
        this.cdr.markForCheck();
      });
  }

  constructor() {
    effect(() => {
      console.log('Effect: обновлены stats', this.stats());
    });
  }
}
