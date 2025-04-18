// resolver-config.component.ts
import { Component, inject, signal } from '@angular/core';
import { PortletConfigApiService } from 'features/portlet/api';
import { FormBuilder, Validators } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

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

  resolverForm = this.fb.group({
    userId: ['', [Validators.required, Validators.min(1)]],
  });

  resolverConfigs: any = null;

  constructor(private fb: FormBuilder) {}

  getResolverConfigs() {
    const userId = Number(this.resolverForm.value.userId);

    this.apiService.getResolverConfigs(userId)
        .then((data) => {
            this.resolverConfigs = data;
            console.log(data);
        })
        .catch((err) => {
            console.error('Ошибка при получении данных:', err);
        });
  }
}
