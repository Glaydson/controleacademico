import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GerenciarPedagogico } from './gerenciar-pedagogico';

describe('GerenciarPedagogico', () => {
  let component: GerenciarPedagogico;
  let fixture: ComponentFixture<GerenciarPedagogico>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [GerenciarPedagogico]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GerenciarPedagogico);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
