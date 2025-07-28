import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GerenciarPedagogicoComponent } from './gerenciar-pedagogico';

describe('GerenciarPedagogico', () => {
  let component: GerenciarPedagogicoComponent;
  let fixture: ComponentFixture<GerenciarPedagogicoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [GerenciarPedagogicoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GerenciarPedagogicoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
