/**
 * Type augmentation to work around a JetBrains IDE issue where forward-referenced
 * interfaces in @angular/core/testing are not properly resolved.
 * (TestBedStatic extends TestBed, but TestBed is defined after TestBedStatic in the .d.ts)
 * See: https://youtrack.jetbrains.com/issues?q=TestBedStatic
 */
import { ComponentFixture, TestModuleMetadata } from '@angular/core/testing';
import { ProviderToken, Type } from '@angular/core';
import { InjectOptions } from '@angular/core';

declare module '@angular/core/testing' {
  interface TestBedStatic {
    configureTestingModule(moduleDef: TestModuleMetadata): TestBedStatic;
    createComponent<T>(component: Type<T>): ComponentFixture<T>;
    resetTestingModule(): TestBedStatic;
    compileComponents(): Promise<any>;
    inject<T>(token: ProviderToken<T>, notFoundValue?: T, options?: InjectOptions): T;
    inject<T>(token: ProviderToken<T>, notFoundValue: null, options?: InjectOptions): T | null;
  }
}

