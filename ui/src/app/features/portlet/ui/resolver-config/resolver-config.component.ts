// resolver-config.component.ts
import { Component, inject, signal } from '@angular/core';
import { PortletConfigApiService } from 'features/portlet/api';
import { FormBuilder, Validators } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ResolverConfigsResponse } from 'features/portlet/models';

@Component({
    standalone: true,
    selector: 'app-resolver-config',
    templateUrl: './resolver-config.component.html',
    styleUrls: ['./resolver-config.component.scss'],
    providers: [PortletConfigApiService],
    imports: [CommonModule, ReactiveFormsModule]
})
export class ResolverConfigComponent {
  readonly apiService = inject(PortletConfigApiService);
  hasFetched = false;


  resolverForm = this.fb.group({
    userId: ['', [Validators.required, Validators.min(1)]],
  });

  resolverConfigEntries: { key: string, values: string[] }[] = [];

  constructor(private fb: FormBuilder) {}

    getResolverConfigs() {
      const userId = Number(this.resolverForm.value.userId);
      this.hasFetched = false;

      this.apiService.getResolverConfigs(userId)
        .then((data: ResolverConfigsResponse) => {
          const configs = data?.resolverConfigs || {};
          this.resolverConfigEntries = Object.entries(configs).map(([key, values]) => ({
            key,
            values,
          }));
        })
        .catch((err) => {
          console.error('Ошибка при получении данных:', err);
          this.resolverConfigEntries = [];
        })
        .finally(() => {
          this.hasFetched = true;
        });
    }
}

