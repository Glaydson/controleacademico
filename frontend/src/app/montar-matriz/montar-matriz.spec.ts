import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MontarMatriz } from './montar-matriz';

describe('MontarMatriz', () => {
  let component: MontarMatriz;
  let fixture: ComponentFixture<MontarMatriz>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MontarMatriz]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MontarMatriz);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
