<template>

  <div>
    <svg-icon class="kindness_class" class-name="kindness-icon" icon-class="aixin"/>
    {{ kindness_value }}
  </div>

</template>

<script>
import {myFundShow} from "@/api/application/myFund";

export default {
  data() {
    return {
      kindness_value: 0,
    }
  },
  created() {
    this.getKindness();
  },

  computed: {
    // getKindness() {
    //   this.kindness_value = 1
    // }
  },
  methods: {
    getKindness() {
      myFundShow().then(response => {

        this.kindness_value = response.data.Score;
      });
    }
  }

}
</script>
