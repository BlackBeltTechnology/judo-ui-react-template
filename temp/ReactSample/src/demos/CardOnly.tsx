/**
 * BASELINE: Card mode (card=true, no collapsible)
 * Maps to: flex.hbs card branch — <Card><CardContent>
 */
import Typography from '@mui/material/Typography';
import Grid from '@mui/material/Grid';
import Stack from '@mui/material/Stack';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Box from '@mui/material/Box';
import { SampleChildrenVertical } from './SampleChildren';

export function CardOnly() {
  return (
    <Box>
      <Typography variant="h5" gutterBottom>Baseline: Card Mode</Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        This is how a Flex renders today with card=true. Uses Card/CardContent.
      </Typography>

      <Grid size={{ xs: 12 }}>
        <Card>
          <CardContent>
            {/* Header with label */}
            <Grid container direction="row" alignItems="center" justifyContent="space-between" spacing={2} sx={{ mb: 2 }}>
              <Grid>
                <Typography variant="h5" component="h1">Card Group Label</Typography>
              </Grid>
            </Grid>
            <Stack spacing={2} className="force-full-width">
              <SampleChildrenVertical />
            </Stack>
          </CardContent>
        </Card>
      </Grid>
    </Box>
  );
}
