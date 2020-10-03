import { WeightedPersonaFactory } from './weighted-persona-factory';

export const selectPersonaFactory = (
  personas: WeightedPersonaFactory[],
  totalWeight: number
): WeightedPersonaFactory => {
  const threshold = Math.floor(Math.random() * totalWeight);

  let total = 0;
  for (let i = 0; i < personas.length; i++) {
    total += personas[i].weight;

    if (total >= threshold) {
      return personas[i];
    }
  }

  return personas[0];
};
