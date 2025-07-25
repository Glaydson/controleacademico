import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VisualizarMatriz } from './visualizar-matriz';

describe('VisualizarMatriz', () => {
  let component: VisualizarMatriz;
  let fixture: ComponentFixture<VisualizarMatriz>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [VisualizarMatriz]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VisualizarMatriz);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
